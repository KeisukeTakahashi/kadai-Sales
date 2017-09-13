import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class SalesSummary {

	public static void main(String[] args) {

		HashMap<String, String> branch1 = new HashMap<String, String>();
		HashMap<String, Long> branch2 = new HashMap<String, Long>();
		HashMap<String, String> commodity1 = new HashMap<String, String>();
		HashMap<String, Long> commodity2 = new HashMap<String, Long>();
		BufferedReader br = null;
		BufferedWriter bw = null;

		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		try {

			File file = new File(args[0], "branch.lst");

			if(file.exists() == false){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}

			br = new BufferedReader(new FileReader(file));
			String buf = br.readLine();
			long gold  = 0;

			while (buf != null) {
				String[] buf2 = buf.split(",", 0);

				//支店コードが数字3文字以外、または要素数が2つ以外ならエラーを出力する
				boolean b = Pattern.matches("^"+java.io.File.separator+"d{3}$", buf2[0]);
				if(b == false || buf2.length != 2){
					System.out.println("エラー：支店定義ファイルのフォーマットが不正です");
					return;
				}

				//ここまでの間に問題がなければマップに格納していく
				branch1.put(buf2[0], buf2[1]);
				branch2.put(buf2[0], gold);
				buf = br.readLine();
			}

			System.out.println(branch1.entrySet());
			System.out.println(branch2.entrySet());

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			if (br != null)
				try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		}

		try {

			File file = new File(args[0], "commodity.lst");

			if(file.exists() == false){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}

			br = new BufferedReader(new FileReader(file));
			String buf = br.readLine();
			long gold  = 0;

			while (buf != null) {
				String[] buf2 = buf.split(",", 0);

				//商品コードが[STF+数字5文字]以外、または要素数が2つ以外ならエラーを出力する
				boolean b = Pattern.matches("^"+"SFT"+java.io.File.separator+"d{5}$", buf2[0]);
				if(b == false || buf2.length != 2){
					System.out.println("エラー：商品定義ファイルのフォーマットが不正です");
					return;
				}

				//ここまでの間に問題がなければマップに格納していく
				commodity1.put(buf2[0], buf2[1]);
				commodity2.put(buf2[0], gold);
				buf = br.readLine();
			}

			System.out.println(commodity1.entrySet());
			System.out.println(commodity2.entrySet());

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			if (br != null)
				try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		}

		//指定フォルダ内にあるアイテムの名前が[数字8桁.rcd]でファイルなら名前を保持する
		File[] files = new File(args[0]).listFiles();
		ArrayList<String> fn12 = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			boolean b = Pattern.matches("^"+java.io.File.separator+"d{8}"+".rcd$",files[i].getName());
			if (b == true && files[i].isFile()) {
				fn12.add((files[i].getName()));
				System.out.println(files[i].getName());
			}
		}

		//連番チェックを行う
		System.out.println("");
		System.out.println("連番チェック");

		//先頭の数字を格納しておき、2つ目からループを開始する
		String f = fn12.get(0).substring(0, 8);
		int num1 = Integer.parseInt(f);

		for(int i = 1; i < fn12.size(); i++) {
			f = fn12.get(i).substring(0, 8);
			int num2 = Integer.parseInt(f);
			if(num2 != num1+1){
				System.out.println("エラー：売上ファイル名が連番になっていません");
				return;
			}
			num1++;
		}
		System.out.println("OK");

		// 8文字のrcdファイルの数だけループして、金額データを支店商品別のmapに格納する
		for (int i = 0; i < fn12.size(); i++) {

			//マップの中に合計金額を格納するテスト
			System.out.println("");
			System.out.println("合計金額の格納を確認");
			System.out.println("ループ回数"+i);
			try {

				File file = new File(args[0], fn12.get(i));
				br = new BufferedReader(new FileReader(file));
				ArrayList<String> rcdlist = new ArrayList<String>();
				String buf = br.readLine();
				int count = 0;

				while (buf != null) {
					rcdlist.add(new String(buf));
					buf = br.readLine();
					count++;

					//rcdファイルの中身の行数が3行でなければエラーを出力する
					if(count >= 4 || buf == null && count <= 2){
						System.out.println("エラー："+fn12.get(i)+"のフォーマットが不正です");
						return;
					}
				}
				System.out.println("デバッグ用：金額は"+rcdlist.get(2));

				//rcdファイル内の金額に文字列や記号が入った場合にエラーを出力する
				boolean b = Pattern.matches("^[0-9]*$",rcdlist.get(2));
				if(b == false) {
					System.out.println("エラー：数字以外の文字が金額の値に使用されています");
					return;
				}

				//rcdファイル内の支店コードがbranch.lst内に存在すればループを抜ける
				int keycount1 = 0;
				for(String Key : branch2.keySet()) {
					if(Key.equals(rcdlist.get(0))){
						System.out.println("デバッグ用：true_b");
						break;
					}
					keycount1++;

					//rcdファイル内の支店コードがbranch.lst内に存在しなければエラーを出力する
					if(keycount1 == branch2.size()){
						System.out.println("デバッグ用：false_b");
						System.out.println("エラー："+fn12.get(i)+"の支店コードが不正です");
						return;
					}
				}

				//rcdファイル内の商品コードがcommodity.lst内に存在すればループを抜ける
				int keycount2 = 0;
				for(String Key : commodity2.keySet()) {
					if(Key.equals(rcdlist.get(1))){
						System.out.println("デバッグ用：true_c");
						break;
					}
					keycount2++;

					//rcdファイル内の商品コードがcommodity.lst内に存在しなければエラーを出力する
					if(keycount2 == commodity2.size()){
						System.out.println("デバッグ用：false_c");
						System.out.println("エラー："+fn12.get(i)+"の商品コードが不正です");
						return;
					}
				}

				// rcdファイル内の金額データを各mapの中に格納する
				branch2.put(rcdlist.get(0), branch2.get(rcdlist.get(0)) + Long.parseLong(rcdlist.get(2)));
				commodity2.put(rcdlist.get(1), commodity2.get(rcdlist.get(1)) + Long.parseLong(rcdlist.get(2)));

				long g=10000000000l; //整数リテラルはint型なので末尾にlを付与してlong型に変換
				if(branch2.get(rcdlist.get(0))>=g || commodity2.get(rcdlist.get(1))>=g) {
					System.out.println("エラー：合計金額が10桁を超えました");
					return;
				}

			} catch (FileNotFoundException e) {
				System.out.println(e);
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (br != null)
					try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
			}

			// 金額データが格納されてるかを確認する
			System.out.println(branch2.entrySet());
			System.out.println(commodity2.entrySet());
		}

		//金額を降順にするテスト
		List<Map.Entry<String,Long>> entries_b2 = new ArrayList<Map.Entry<String,Long>>(branch2.entrySet());
		Collections.sort(entries_b2, new Comparator<Map.Entry<String,Long>>() {

	            public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
	                return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
	            }
	        });

		//降順ソートの確認テスト：branch
		System.out.println("");
		System.out.println("降順ソートの確認：branch");
		for (Entry<String,Long> s : entries_b2) {
            System.out.println("s.getKey() : " + s.getKey());
            System.out.println("s.getValue() : " + s.getValue());
        }

		//金額を降順にするテスト
		List<Map.Entry<String,Long>> entries_c2 = new ArrayList<Map.Entry<String,Long>>(commodity2.entrySet());
		Collections.sort(entries_c2, new Comparator<Map.Entry<String,Long>>() {

	            public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
	                return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
	            }
	        });

		//降順ソートの確認テスト：commodity
		System.out.println("");
		System.out.println("降順ソートの確認：commodity");
		for (Entry<String,Long> s : entries_c2) {
            System.out.println("s.getKey() : " + s.getKey());
            System.out.println("s.getValue() : " + s.getValue());
        }
		System.out.println("");
		System.out.println("ファイルの出力確認");

		//ファイルの書き出し：branch
		try {
			File file = new File(args[0], "branch.out");
			bw = new BufferedWriter(new FileWriter(file));

			for (Entry<String,Long> s : entries_b2) {
			bw.write(s.getKey()+","+branch1.get(s.getKey())+","+s.getValue());
			bw.newLine();
			}
			System.out.println("branch.outを出力しました");

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			if (br != null)
				try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		}

		//ファイルの書き出し：commodity
		try {
			File file = new File(args[0], "commodity.out");
			bw = new BufferedWriter(new FileWriter(file));

			for (Entry<String,Long> s : entries_c2) {
			bw.write(s.getKey()+","+commodity1.get(s.getKey())+","+s.getValue());
			bw.newLine();
			}
			System.out.println("commodity.outを出力しました");

		} catch (FileNotFoundException e) {
			System.out.println(e);
		}catch (IOException e) {
			System.out.println(e);
		} finally {
			if (br != null)
				try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		}
	}
}