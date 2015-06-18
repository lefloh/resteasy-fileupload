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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Hirsch
 */
@Path("/file")
public class FileUploadResource {

	private Logger LOG = LoggerFactory.getLogger(FileUploadResource.class);
	
	/**
	 * Resteasy determines the charset from the Content-Type associated with a part.
	 * If set correctly everything is working out of the box.
	 * HTML Spec: http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2

		POST /file HTTP/1.1
		Content-Type: multipart/form-data; boundary=AaB03x

   		--AaB03x
   		Content-Disposition: form-data; name="file"; filename="file1.txt"
   		Content-Type: text/plain; charset=utf-8

   		... contents of file1.txt ...
   		--AaB03x--

	 * @return the content of the input part "file" as text. 
	 * Hopefully in the same encoding as sent.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public Response upload(MultipartFormDataInput input, @Context HttpHeaders headers) throws IOException {
		InputPart file = input.getFormDataMap().get("file").get(0);
		String content = file.getBodyAsString();
		LOG.info(String.format("Received '%s' with MediaType '%s'", content, file.getMediaType()));
	    return Response.ok(content).header("Content-Type", file.getMediaType().toString()).build();
	}

	/**
	 * It seems not to be possible to control the Content-Type header of a single part element.
	 * The multipart/form-data encoding algorithm for HTML5 is described here:
	 * http://dev.w3.org/html5/spec-preview/constraints.html#multipart-form-data
	 * One possible way to work with different charsets is adding a input type=hidden with name=_charset_
	 * to the according form. The browser should populate this field with the encoding defined on the form
	 * or for the whole HTML page.
	 * @return the content of the input part "file" as text. 
	 * Hopefully in the same encoding as sent.
	 */
	@POST
	@Path("/form")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public Response uploadForm(MultipartFormDataInput input) throws IOException {
		String charset = input.getFormDataMap().get("_charset_").get(0).getBodyAsString();
		InputPart file = input.getFormDataMap().get("file").get(0);
		InputStream inputStream = file.getBody(InputStream.class, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset));
		String line;
		StringBuilder content = new StringBuilder();
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
		LOG.info(String.format("Received '%s' with Charset '%s'", content.toString(), charset));
	    return Response.ok(content).header("Content-Type", MediaType.TEXT_PLAIN_TYPE.withCharset(charset)).build();
	}
	
}
