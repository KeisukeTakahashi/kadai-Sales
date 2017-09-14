package jp.alhinc.takahashi_keisuke.calculate_sales;

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

public class Sales {

	public static void main(String[] args) {

		HashMap<String, String> branch1 = new HashMap<String, String>();
		HashMap<String, Long> branch2 = new HashMap<String, Long>();
		HashMap<String, String> commodity1 = new HashMap<String, String>();
		HashMap<String, Long> commodity2 = new HashMap<String, Long>();
		BufferedReader br = null;

		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		if(!reader(args[0], "branch.lst", "^\\d{3}$", "支店定義", branch1, branch2)) {
			return;
		}

		if(!reader(args[0], "commodity.lst", "^[a-zA-Z0-9]{8}$", "商品定義", commodity1, commodity2)) {
			return;
		}

		// 指定フォルダ内にあるアイテムの名前が[数字8桁.rcd]でファイルなら名前を保持する
		File[] files = new File(args[0]).listFiles();
		ArrayList<String> fn12 = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().matches("^\\d{8}.rcd$") && files[i].isFile()) {
				fn12.add((files[i].getName()));
			}
		}


		if (fn12.size() > 0) {

			// 先頭の数字を格納しておき、2つ目からループをさせて連番チェックを行う
			String f = fn12.get(0).substring(0, 8);
			int num1 = Integer.parseInt(f);

			for (int i = 1; i < fn12.size(); i++) {
				f = fn12.get(i).substring(0, 8);
				int num2 = Integer.parseInt(f);
				if (num2 != num1 + 1) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
				num1++;
			}
		}

		// 8文字のrcdファイルの数だけループして、金額データを支店商品別のmapに格納する
		for (int i = 0; i < fn12.size(); i++) {

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

					// rcdファイルの中身の行数が3行でなければエラーを出力する
					if (count >= 4 || buf == null && count <= 2) {
						System.out.println(fn12.get(i) + "のフォーマットが不正です");
						return;
					}
				}

				// rcdファイル内の金額に文字列や記号が入った場合にエラーを出力する
				if (!rcdlist.get(2).matches("^[0-9]*$")) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				// rcdファイル内の支店コードがbranch.lst内に存在すればループを抜ける
				int keycount1 = 0;
				for (String Key : branch2.keySet()) {
					if (Key.equals(rcdlist.get(0))) {
						break;
					}
					keycount1++;

					// rcdファイル内の支店コードがbranch.lst内に存在しなければエラーを出力する
					if (keycount1 == branch2.size()) {
						System.out.println(fn12.get(i) + "の支店コードが不正です");
						return;
					}
				}

				// rcdファイル内の商品コードがcommodity.lst内に存在すればループを抜ける
				int keycount2 = 0;
				for (String Key : commodity2.keySet()) {
					if (Key.equals(rcdlist.get(1))) {
						break;
					}
					keycount2++;

					// rcdファイル内の商品コードがcommodity.lst内に存在しなければエラーを出力する
					if (keycount2 == commodity2.size()) {
						System.out.println(fn12.get(i) + "の商品コードが不正です");
						return;
					}
				}

				// rcdファイル内の金額データを各mapの中に格納する
				branch2.put(rcdlist.get(0), branch2.get(rcdlist.get(0)) + Long.parseLong(rcdlist.get(2)));
				commodity2.put(rcdlist.get(1), commodity2.get(rcdlist.get(1)) + Long.parseLong(rcdlist.get(2)));

				// 整数リテラルをlong型に変換
				long g = 10000000000L;
				if (branch2.get(rcdlist.get(0)) >= g || commodity2.get(rcdlist.get(1)) >= g) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

			} catch (FileNotFoundException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			} finally {
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
			}
		}
		if(!writer(args[0], "branch.out", branch1, branch2)) {
			return;
		}

		if(!writer(args[0], "commodity.out", commodity1, commodity2)) {
			return;
		}
	}

	public static boolean reader(String args, String name, String reg, String defname,  HashMap<String, String> map1,
			HashMap<String, Long> map2) {

		BufferedReader br = null;

		try {

			File file = new File(args, name);

			if (file.exists() == false) {
				System.out.println(defname+"ファイルが存在しません");
				return false;
			}

			br = new BufferedReader(new FileReader(file));
			String buf = br.readLine();
			long gold = 0;

			while (buf != null) {
				String[] buf2 = buf.split(",", 0);

					//各コードのファーマットが不正、または要素数が2つ以外ならエラーを出力する
					if (!buf2[0].matches(reg) || buf2.length != 2) {
						System.out.println(defname+"ファイルのフォーマットが不正です");
						return false;
					}

				// ここまでの間に問題がなければマップに格納する
				map1.put(buf2[0], buf2[1]);
				map2.put(buf2[0], gold);
				buf = br.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
		}
		return true;
	}

	public static boolean writer(String args, String name, HashMap<String, String> map1, HashMap<String, Long> map2) {
		// 金額を降順にする
		List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(map2.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {

			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}
		});

		// ファイルの書き出し
		BufferedWriter bw = null;

		try {
			File file = new File(args, name);
			bw = new BufferedWriter(new FileWriter(file));

			for (Entry<String, Long> s : entries) {
				bw.write(s.getKey() + "," + map1.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
		}
		return true;
	}
}
