package io.github.omaraalqarni.common;

public class ApiException extends RuntimeException{
  private final int statusCode;
  private final String apiErrorCode;


  public ApiException(String message, int StatusCode ){
    super(message);
    this.statusCode = StatusCode;
    this.apiErrorCode = null;
  }

  public ApiException(String message, int statusCode, String apiErrorCode) {
    super(message);
    this.statusCode = statusCode;
    this.apiErrorCode = apiErrorCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getApiErrorCode() {
    return apiErrorCode;
  }
}
