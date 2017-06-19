package com.proxy.kiwi.app;

import java.io.File;

import com.proxy.kiwi.core.utils.Stopwatch;
import com.proxy.kiwi.core.v2.folder.FileFolderV2;
import com.proxy.kiwi.core.v2.folder.FolderV2;
import com.proxy.kiwi.core.v2.folder.FoldersV2;
import com.proxy.kiwi.core.v2.service.GenericTaskService;
import com.proxy.kiwi.core.v2.service.Service;

import ch.qos.logback.core.net.SyslogOutputStream;
import javafx.application.Application;
import javafx.stage.Stage;

public class Test extends Application{
	public static void main(String[] args) {
//		launch(args);
		
		Service.init(GenericTaskService.class);
		
		Stopwatch.click("New folder creation");
		FolderV2 root = new FileFolderV2(new File("S:\\Cloud\\Hens"), "root", null);
		Stopwatch.click("New folder creation");


		Stopwatch.click("Extra waiting");

		while (GenericTaskService.getQueueSize() > 0) {
			try {
//				root.refactor();
//				System.out.println(FoldersV2.DFS(root).count());
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		Stopwatch.click("Extra waiting");

//		root.refactor();
		System.out.println(FoldersV2.DFS(root).count());
		FoldersV2.DFS(root).limit(40).forEach(f -> {
			System.out.println(f.getName() +  "[ " + f.folderStream().count() + " | " + f.imageStream().count() + " ]");
		});

		System.exit(0);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

	}
}
