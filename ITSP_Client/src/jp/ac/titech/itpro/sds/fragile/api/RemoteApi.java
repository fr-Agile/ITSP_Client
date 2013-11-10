package jp.ac.titech.itpro.sds.fragile.api;

import java.io.IOException;

import jp.ac.titech.itpro.sds.fragile.utils.CommonUtils;

import com.appspot.fragile_t.friendEndpoint.FriendEndpoint;
import com.appspot.fragile_t.getFriendEndpoint.GetFriendEndpoint;
import com.appspot.fragile_t.getShareTimeEndpoint.GetShareTimeEndpoint;
import com.appspot.fragile_t.groupEndpoint.GroupEndpoint;
import com.appspot.fragile_t.loginEndpoint.LoginEndpoint;
import com.appspot.fragile_t.registerEndpoint.RegisterEndpoint;
import com.appspot.fragile_t.registrationIdEndpoint.RegistrationIdEndpoint;
import com.appspot.fragile_t.repeatScheduleEndpoint.RepeatScheduleEndpoint;
import com.appspot.fragile_t.scheduleEndpoint.ScheduleEndpoint;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;


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
	
	public static RegistrationIdEndpoint getRegistrationIdEndpoint() {
		RegistrationIdEndpoint.Builder endpointBuilder = new RegistrationIdEndpoint.Builder(
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
	
	public static GetShareTimeEndpoint getGetShareTimeEndpoint() {
		GetShareTimeEndpoint.Builder endpointBuilder = new GetShareTimeEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	
	public static GetFriendEndpoint getGetFriendEndpoint() {
		GetFriendEndpoint.Builder endpointBuilder = new GetFriendEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	
	public static GroupEndpoint getGroupEndpoint() {
		GroupEndpoint.Builder endpointBuilder = new GroupEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
	
	public static RepeatScheduleEndpoint getRepeatScheduleEndpoint() {
		RepeatScheduleEndpoint.Builder endpointBuilder = new RepeatScheduleEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), 
				new JacksonFactory(), 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				}); 
		
		return updateBuilder(endpointBuilder).build();
	}
}
