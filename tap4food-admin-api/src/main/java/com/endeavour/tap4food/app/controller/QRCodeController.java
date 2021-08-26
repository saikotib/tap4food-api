package com.endeavour.tap4food.app.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.QRCodeService;
import com.google.zxing.WriterException;

@RestController
@RequestMapping("/api/admin/qrcode")
public class QRCodeController {
	
	@Autowired
	private QRCodeService qrCodeService;

	@RequestMapping(value = "/generate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createQRCode(@RequestParam("foodcourtid") String foodCourtId) throws WriterException, IOException{
		
		String qrImage =  qrCodeService.generateQRCodeImage(foodCourtId);
		
		ResponseHolder response = ResponseHolder.builder()
				.status("success")
				.data(qrImage)
				.build();
		
		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/regenerate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> reCreateQRCode(){
		return null;
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deleteQRCode(){
		return null;
	}
}
