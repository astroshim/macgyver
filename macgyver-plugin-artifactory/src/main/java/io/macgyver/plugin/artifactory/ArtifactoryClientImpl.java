package io.macgyver.plugin.artifactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.squareup.okhttp.OkHttpClient;

import io.macgyver.core.rest.BasicAuthInterceptor;
import io.macgyver.okrest.OkRestClient;
import io.macgyver.okrest.OkRestResponse;
import io.macgyver.okrest.OkRestTarget;

public class ArtifactoryClientImpl implements ArtifactoryClient {


	OkRestClient okRestClient;
	OkRestTarget base;
	String username;
	String password;
	public ArtifactoryClientImpl(String url, String username, String password) {
	
		this.username = username;
		this.password = password;
		
		okRestClient = new OkRestClient();
		
		okRestClient.getOkHttpClient().interceptors().add(new BasicAuthInterceptor(username, password));
		base = okRestClient.uri(url);
		
		
		
	}

	String getPassword() {
		return password;
	}
	String getUsername() {
		return username;
	}
	@Override
	public OkRestTarget getBaseTarget() {
		return base;
	}

	@Override
	public GavcSearchBuilder gavcSearch() {
		return new GavcSearchBuilder(getBaseTarget());
	}
	public PropertySearchBuilder propertySearch() {
		return new PropertySearchBuilder(getBaseTarget());
	}
	
	public DateSearchBuilder dateSearch() {
		return new DateSearchBuilder(getBaseTarget());
	}

	@Override
	public InputStream fetchArtifact(String path) throws IOException {
		 OkRestResponse rr = base.path(path).get().execute();
		
		 InputStream is = rr.getBody(InputStream.class);
		 
		
		return is;
	}

	@Override
	public void delete(String path) throws IOException{
		base.path(path).delete().execute();
	}

	@Override
	public File fetchArtifactToFile(String path, File out) throws IOException {
		Closer closer = Closer.create();
		try {
			BufferedInputStream is = new BufferedInputStream(fetchArtifact(path));
			closer.register(is);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
			closer.register(bos);
			
			ByteStreams.copy(is, bos);
		}
		finally {
			closer.close();
		}
		
		return out;
	}
	
	@Override
	public File fetchArtifactToDir(String path, File targetDir) throws IOException {
		List<String> split = Splitter.on('/').omitEmptyStrings().splitToList(path);
		String x = split.get(split.size()-1);
		File f = new File(targetDir,x);
		
		return fetchArtifactToFile(path,f);
		
	
	}
	
	@Override
	public File fetchArtifactToTempFile(String path) throws IOException {
		List<String> split = Splitter.on('/').omitEmptyStrings().splitToList(path);
		String x = split.get(split.size()-1);
		
		File f = new File(Files.createTempDir(),x);
		
		return fetchArtifactToFile(path, f);
	}
}