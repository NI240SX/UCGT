package testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CtkPartsGen {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("CTK-Parts.txt")));
			
			

			BufferedReader br = new BufferedReader(new FileReader(new File("data/Parts_KIT00")));
			String part;
			while ((part = br.readLine())!=null){
				bw.write("\n"+ "KIT00_" + part + "_A");
				bw.write("\n"+"KIT00_" + part + "_B");
				bw.write("\n"+"KIT00_" + part + "_C");
				bw.write("\n"+"KIT00_" + part + "_D");
				for (int as=0; as<11; as++) {
					bw.write("\n"+"KIT00_" + part + "_T" + as + "_A");
					bw.write("\n"+"KIT00_" + part + "_T" + as + "_B");
					bw.write("\n"+"KIT00_" + part + "_T" + as + "_C");
					bw.write("\n"+"KIT00_" + part + "_T" + as + "_D");
				}
			}
			
			
			
			
			
			for (int k=0; k<50; k++) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));

				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KIT0"+k;
					else kstr = "KIT"+k;
					bw.write("\n"+ kstr + "_" + part + "_A");
					bw.write("\n"+kstr + "_" + part + "_B");
					bw.write("\n"+kstr + "_" + part + "_C");
					if(k==0) bw.write("\n"+kstr + "_" + part + "_D");

					
					for (int as=0; as<11; as++) {
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_A");
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_B");
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_C");
						if(k==0) bw.write("\n"+kstr + "_" + part + "_T" + as + "_D");
					}
				}
				
				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));

				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KIT0"+k;
					else kstr = "KIT"+k;
					for (int as=0; as<50; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						bw.write("\n"+kstr + "_" + part + nstr + "_A");
						bw.write("\n"+kstr + "_" + part + nstr + "_B");
						bw.write("\n"+kstr + "_" + part + nstr + "_C");
						if(k==0) bw.write("\n"+kstr + "_" + part + nstr + "_D");
					}
				}
				
			}
			
			for (int k=0;k<50;k++) {
				br = new BufferedReader(new FileReader(new File("data/Parts_anykit")));

				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KITW0"+k;
					else kstr = "KITW"+k;
					bw.write("\n"+kstr + "_" + part + "_A");
					bw.write("\n"+kstr + "_" + part + "_B");
					bw.write("\n"+kstr + "_" + part + "_C");

					for (int as=0; as<11; as++) {
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_A");
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_B");
						bw.write("\n"+kstr + "_" + part + "_T" + as + "_C");
					}
				}

				br = new BufferedReader(new FileReader(new File("data/Parts_exhaust")));

				while ((part = br.readLine())!=null){
				
					String kstr;
					if(k<10)kstr = "KITW0"+k;
					else kstr = "KITW"+k;
					for (int as=0; as<50; as++) {
						String nstr;
						if(as<10)nstr = "_0"+as;
						else nstr = "_"+as;
						bw.write("\n"+kstr + "_" + part + nstr + "_A");
						bw.write("\n"+kstr + "_" + part + nstr + "_B");
						bw.write("\n"+kstr + "_" + part + nstr + "_C");
						if(k==0) bw.write("\n"+kstr + "_" + part + nstr + "_D");
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

}
