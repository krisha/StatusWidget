package org.backspace.doorstatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class DoorState {
	
	public enum State {
		Unknown,
		Closed,
		Open
	}
	
	public State status;
	public int members;
	
	public DoorState ()
	{
		status = State.Unknown;
		members = 0;
	}
	
	public String humanReadableStringGet ( )
	{	
		switch ( status )
		{
		case Closed:
			return "bckspc\nclosed";
		case Open:
			return "bckspc\n" + members;
		case Unknown:
			return "bckspc\nerror";
		}
		
		/* never reached */
		return "error";

	}
	
	/**
	 * update member variables from website
	 * @return false if no inet access, unrecognized string on website
	 */
	public boolean statusUpdate ( String url )
	{		
		HttpClient client = new DefaultHttpClient();
		try
		{
			/* sychronous request will abort after a few seconds, exception thrown */
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			HttpResponse resp = client.execute(request);
			
			if ( resp.getStatusLine().getStatusCode() == 200 )
			{
				BufferedReader in = new BufferedReader(new InputStreamReader( resp.getEntity().getContent()));
				String line;
				String complete = "";
				while ( ( line = in.readLine() ) != null )
					complete += line; 
				
				Log.w("DoorState", complete );
				
				complete = complete.replace("members: ", "" );
				complete = complete.replace(" ", "");
				
				members = Integer.valueOf(complete);
				
				if ( members == 0 )
				{
					status = State.Closed;
				}
				else
					status = State.Open;
				
				Log.w("DoorState", "len: " + complete.length() + " int " + members);
				
			}
		} catch ( Exception e )
		{
			Log.w("DoorState", "Exception " + e );
			status = State.Unknown;
			return false;
		}
		
		return true;
	}

}
