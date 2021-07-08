package com.endeavour.tap4food.app.util;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

public class AvatarImage {
	
	public byte[] avatarImage() throws IOException {		
		return StreamUtils.copyToByteArray((new ClassPathResource("Images/Avatar.jpeg")).getInputStream());
	}

}
