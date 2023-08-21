package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.controller.MainController;

@SpringBootApplication
public class TestShapefileReaderApplication implements CommandLineRunner {

	@Autowired
	private MainController mainController;

	public static void main(String[] args) {
		SpringApplication.run(TestShapefileReaderApplication.class, args);
	}
	
	@Override
	public void run(String... strings){

		mainController.start();
	}
}
