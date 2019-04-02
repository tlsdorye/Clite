import java.io.*;

public class Lexer {
	private boolean isEof = false;
	private char ch = ' ';
	private BufferedReader input;
	private String line = "";
	private int lineno = 0;
	private int col = 1;
	private final String letters = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final String digits = "0123456789";
	private final char eolnCh = '\n';
	private final char eofCh = '\004';

	public Lexer(String fileName) { // source filename
		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
			System.exit(1);
		}
	}

	private char nextChar() { // Return next char
		if (ch == eofCh)
			error("Attempt to read past end of file");
		col++;
		if (col >= line.length()) {
			try {
				line = input.readLine();
			} catch (IOException e) {
				System.err.println(e);
				System.exit(1);
			} // try
			if (line == null) // at end of file
				line = "" + eofCh;
			else {
				// System.out.println(lineno + ":\t" + line);
				lineno++;
				line += eolnCh;
			} // if lone
			col = 0;
		} // if col
		return line.charAt(col);
	}

	public Token next() { // Return next token
		do {
			if (isLetter(ch)) { // ident or keyword
				String spelling = concat(letters + digits);
				return Token.keyword(spelling);
			} else if (isDigit(ch)) { // int or float literal
				String number = concat(digits);
				if (ch != '.') // int Literal
					return Token.mkIntLiteral(number);
				number += concat(digits);
				return Token.mkFloatLiteral(number);
			} else
				switch (ch) {
				case ' ':
				case '\t':
				case '\r':
				case eolnCh:
					ch = nextChar();
					break;
				case '/':
					ch = nextChar();
					// divide
					if (ch != '/')
						return Token.divideTok;
					// comment
					do {
						ch = nextChar();
					} while (ch != eolnCh);
					ch = nextChar();
					break;
				case '\'':
					char ch1 = nextChar();
					nextChar(); // get '
					ch = nextChar();
					return Token.mkCharLiteral("" + ch1);
				case eofCh:
					return Token.eofTok;
				case '+':
					ch = nextChar();
					return Token.plusTok;
				case '-':
					ch = nextChar();
					return Token.minusTok;
				case '*':
					ch = nextChar();
					return Token.multiplyTok;
				case '(':
					ch = nextChar();
					return Token.leftParenTok;
				case ')':
					ch = nextChar();
					return Token.rightParenTok;
				case '{':
					ch = nextChar();
					return Token.leftBraceTok;
				case '}':
					ch = nextChar();
					return Token.rightBraceTok;
				case '&':
				case '=':
				case '<':
				case '>':
				case '!':
				}
		} while (true);
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private void check(char c) {
		ch = nextChar();
		if (ch != c)
			error("illegal character, expecting " + c);
		ch = nextChar();
	}

	private Token chkOpt(char c, Token one, Token two) {
		return null; // student exercise
	}

	private String concat(String set) {
		String r = "";
		do {
			r += ch;
			ch = nextChar();
		} while (set.indexOf(ch) >= 0);
		return r;
	}

	public void error(String msg) {
		System.err.println(line);
		System.err.println("Error: column " + col + " " + msg);
		System.exit(1);
	}

	static public void main(String[] argv) {

		
	}
}
