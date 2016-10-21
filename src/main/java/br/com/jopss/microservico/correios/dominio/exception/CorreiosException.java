package br.com.jopss.microservico.correios.dominio.exception;

public class CorreiosException extends RuntimeException {

	private static final long serialVersionUID = 1112813749631064010L;

	private int code;
	private String html;

        public CorreiosException(String message) {
                super(message);
        }

	public CorreiosException() {
		super();
	}

	public CorreiosException(int code, String s) {
		super(s);
		this.code = code;
	}

	public CorreiosException(int code, String s, String html) {
		super(s);
		this.code = code;
		this.html = html;
	}

	public CorreiosException(Exception e) {
		super(e);
	}

	public String getHtml() {
		return html;
	}

	public boolean codeIsHTTPRedirect() {
		return code == 302 || code == 301;
	}

	public boolean codeIsHTTPNotFound() {
		return code == 404;
	}

	public boolean codeIsHTTPNotAuthorized() {
		return code == 401;
	}

}
