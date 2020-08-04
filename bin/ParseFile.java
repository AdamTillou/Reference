import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ParseFile {

	public static void main (String[] args) {
		List<String> lines = getLineList("vim.txt");
		List<String> header = getLineList("header");

		for (String q : header) {
			System.out.println(q);
 		}

		System.out.println("<html>");
		System.out.println(printTabs(1) + "<h1>" + lines.get(0) + "</h1>");

		int id = 1;
		for (int i = 1; i < lines.size(); i++) {
			String input = lines.get(i);
			int indent = getIndent(input);
			int nextIndent = 0;
			if (i < lines.size() - 1) {
				nextIndent = getIndent(lines.get(i + 1));
			}

			boolean expandable = indent < nextIndent;

			String text = input.substring(indent, input.length());
			if (text.length() == 0) {
				continue;
			}

			if (expandable) {
				System.out.println(printTabs(indent + 1) + getHtml(text, id, indent, true));
				System.out.println(printTabs(indent + 1) + String.format("<div id='%s'>", id));
				id++;
			} else {
				System.out.println(printTabs(indent + 1) + getHtml(text, 0, indent, false));
				if (indent > nextIndent) {
					int closeCt = indent - nextIndent;
					for (int j = 0; j < closeCt; j++) {
						System.out.println(printTabs(indent - j) + "</div>");
					}
				}
			}
		}
		System.out.println("</html>");
	}

	private static List<String> getLineList(String path) {
		try {
			return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getHtml(String input, int id, int indent, boolean expandable) {
		String text = "";
		for (int i = 0; i < input.length(); i++) { 
			char q = input.charAt(i);
			if (q == '<') {
				text += "&lt";
			} else if (q == '>') {
				text += "&gt";
			} else {
				text += q;
			}
		}

		// Format bold text
		if (text.substring(0, 1).equals("-")) {
			text = text.substring(2, text.length());
		} else if (text.contains(":")) {
			int split = -1;
			for (int i = 0; i < text.length() - 2; i++) {
				if (text.charAt(i) == ':' && text.charAt(i + 1) == ' ') {
					split = i;
					break;
				}
			}
			if (split != -1) {
				text = "<b>" + text.substring(0, split + 2) + "</b>" + text.substring(split + 2, text.length());
			}
		} else {
			text = "<b>" + text + "</b>";
		}

		// Format code blocks
		while (true) {
			Pattern r = Pattern.compile("(.*)``(.*)``(.*)");
			Matcher m = r.matcher(text);
			if (m.find( )) {
				text = m.group(1) + "<code>" + m.group(2) + "</code>" + m.group(3);
			} else {
				break;
			}
		}

		if (!expandable) {
			return String.format("<span style='margin-left:%2$dpx'>%1$s</span><br/>", text, ((indent + 1) * 20));
		}

		String arrow = String.format("<span id='%1$d-btn' class='arrow'>▹ </span> ", id);
		String span1 = String.format("<span id='%1$d-text' class='btn' style='margin-left:%2$dpx'><span style='cursor:pointer' onclick=toggle('%1$s')>", id, (indent + 1) * 20);
		String span2 = "</span></span><br/>";

		return span1 + arrow + " " + text + span2;
	}

	private static int getIndent(String input) {
		if (input.length() == 0) {
			return 0;
		}

		int indent = 0;
		for (int i = 0; input.charAt(i) == '	'; i++) {
			indent += 1;
		}

		return indent;
	}

	private static String printTabs(int num) {
		String tabs = "";
		for (int i = 0; i < num; i++) {
			tabs += "	";
		}
		return tabs;
	}
}
