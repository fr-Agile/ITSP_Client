package jp.ac.titech.itpro.sds.fragile.api;

import java.io.IOException;

import jp.ac.titech.itpro.sds.fragile.utils.CommonUtils;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.loginEndpoint.LoginEndpoint;
import com.google.api.services.registerEndpoint.RegisterEndpoint;
import com.google.api.services.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.services.friendEndpoint.FriendEndpoint;

public class RemoteApi {
	protected static <B extends AbstractGoogleClient.Builder> B updateBuilder(B builder) {
		builder.setRootUrl(CommonUtils.BASE_URL + "_ah/api/");

		final boolean enableGZip = builder.getRootUrl().startsWith("https:");

		builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
			public void initialize(AbstractGoogleClientRequest<?> request)
					throws IOException {
				if (!enableGZip) {
					request.setDisableGZipContent(true);
				}
			}
		});

		return builder;
	}

	public static LoginEndpoint getLoginEndpoint() {
		LoginEndpoint.Builder endpointBuilder = new LoginEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				});

		return updateBuilder(endpointBuilder).build();
	}
	
	public static RegisterEndpoint getRegisterEndpoint() {
		RegisterEndpoint.Builder endpointBuilder = new RegisterEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	
	public static FriendEndpoint getFriendEndpoint() {
		FriendEndpoint.Builder endpointBuilder = new FriendEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	 
	public static ScheduleEndpoint getScheduleEndpoint() {
		ScheduleEndpoint.Builder endpointBuilder = new ScheduleEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	
}
