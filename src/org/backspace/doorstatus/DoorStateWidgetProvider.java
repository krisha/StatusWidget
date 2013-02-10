package org.backspace.doorstatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.backspace.doorstatus.DoorState.State;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class DoorStateWidgetProvider extends AppWidgetProvider {

	/* configuration */
	private static final String url = "http://status.bckspc.de/status.php?response=ascii";
	//private static final String url = "http://192.168.0.107/ae/status.php?response=ascii";
	private static final String notificationTitle = "geöffnet";
	private static final String notificationHeader = "backspace";
	/* time after */
	private static final String notificationText = "ist geöffnet!";
	private static final boolean useNotification = true;
	
	
	
	private static final String ACTION_CLICK = "ACTION_CLICK";
	
	private static final int HELLO_ID = 1;
	private static DoorState lastDoorState = new DoorState(); 
	
	/* close->open => notify
	 * open->close => remove notify if there
	 */
	private void updateNotification ( Context context, DoorState state )
	{
		if ( (lastDoorState.status == State.Closed || lastDoorState.status == State.Unknown ) &&
				state.status == State.Open )
		{
			/* space was opened -> notify */
			
			lastDoorState.status = State.Open;
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( ns );
			
			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = notificationTitle;
			long when = System.currentTimeMillis();
			
			Notification notification = new Notification( icon, tickerText, when );
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			
			//Calendar cal = Calendar.getInstance();
			//SimpleDateFormat formater = new SimpleDateFormat();
			
			CharSequence contentTitle = notificationHeader;
			CharSequence contentText = notificationText; // + formater.format(cal.getTime());
			Intent notificationIntent = new Intent ( context, DoorStateWidgetProvider.class );
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			
			mNotificationManager.notify( HELLO_ID, notification );
			
			
		} else if ( lastDoorState.status == State.Open &&
				(state.status == State.Closed || state.status == State.Unknown) )
		{
			/* space was closed -> remove notify */
			
			lastDoorState.status = state.status;
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( ns );
			
			mNotificationManager.cancel(HELLO_ID);
		}
	}
	
	@Override
	public void onUpdate ( Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		/* Get all IDs */
		ComponentName thisWidget = new ComponentName(context, DoorStateWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		DoorState state = new DoorState();
		state.statusUpdate( url );
		
		if ( useNotification )
			updateNotification(context, state);
		
		for ( int widgetId : allWidgetIds )
		{
			RemoteViews remoteViews = null;
			
			if ( state.status == State.Unknown || state.status == State.Closed )
				remoteViews = new RemoteViews ( context.getPackageName(), R.layout.widget_layout_closed );
			else
				remoteViews = new RemoteViews ( context.getPackageName(), R.layout.widget_layout_open );
	
			remoteViews.setTextViewText(R.id.update, state.humanReadableStringGet() );
			
			Intent intent = new Intent(context, DoorStateWidgetProvider.class);
			
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		
	}
	
}
