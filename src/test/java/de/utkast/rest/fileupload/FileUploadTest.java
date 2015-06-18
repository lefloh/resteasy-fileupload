/**
 * Copyright (C) 2014 Florian Hirsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.utkast.rest.fileupload;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Hirsch
 */
public class FileUploadTest {

	private static final String BASE_URL = "http://localhost:9090/resteasy-fileupload/r/file";
	
	private static final String RESOURCE_PATH = "src/test/resources/de/utkast/rest/fileupload";
	
	private String userDir;
	
	private Client client;
	
	@Before
	public void onBefore() {
		client = ClientBuilder.newClient();
		userDir = System.getProperty("user.dir");
	}
	
	@Test
	public void testMediaType() throws IOException {
		postMediaType("utf-8");
		postMediaType("iso-8859-15");
	}
	
	private void postMediaType(String charset) throws IOException {
		byte[] content = readFile(charset);
		WebTarget target = client.target(BASE_URL);
		MultipartFormDataOutput formData = new MultipartFormDataOutput();
		formData.addFormData("file", content, MediaType.TEXT_PLAIN_TYPE.withCharset(charset));
		Entity<MultipartFormDataOutput> entity = Entity.entity(formData, MediaType.MULTIPART_FORM_DATA);
		Response response = target.request().post(entity);
		assertEquals(new String(content, charset), response.readEntity(String.class));
	}
	
	@Test
	public void testInterceptor() throws IOException {
		byte[] content = readFile("utf-8");
		WebTarget target = client.target(BASE_URL);
		MultipartFormDataOutput formData = new MultipartFormDataOutput();
		formData.addFormData("file", content, MediaType.TEXT_PLAIN_TYPE);
		Entity<MultipartFormDataOutput> entity = Entity.entity(formData, MediaType.MULTIPART_FORM_DATA);
		Response response = target.request().post(entity);
		String utf8content = new String(content);
		assertFalse(utf8content.equals(response.readEntity(String.class)));
		// activate the interceptor
		response = target.request().header("X-Charset","utf-8").post(entity);
		assertEquals(utf8content, response.readEntity(String.class));
	}

	private byte[] readFile(String charset) throws IOException {
		Path path = Paths.get(userDir, RESOURCE_PATH, String.format("%s.txt", charset));
		byte[] content = Files.readAllBytes(path);
		return content;
	}
	
}
