import java.io.File;
			
			String targetPath = "C:/workspace/IS_STAT_REC/webapps/media";
			File dirFile = new File(targetPath);
			File[] fileList = dirFile.listFiles();
			int allowInterval = 1*60*60*1; // 최종수정시간이 1 시간지난 파일 대상
			
			long now = System.currentTimeMillis();
			long fileTime = 0L;
			for (File file : fileList) {
				if( file.isFile() ) {
					fileTime = file.lastModified();
					long elapse = (now - fileTime)/1000;
					System.out.println(file.getName() + " : " + elapse);
					if( elapse > allowInterval ) {
						file.delete();
					}
				}
			}
