package com.se114p12.backend.constants;

public class AppConstant {
  public static final String DOMAIN = "http://192.168.88.67:8080";
  public static final String API_VERSION = "v1";
  public static final String API_BASE_PATH = "/api/" + API_VERSION;
  public static final String BACKEND_URL = DOMAIN + API_BASE_PATH;
  public static final String CATEGORY_FOLDER = "category";
  public static final String USER_FOLDER = "user";
  public static final String PRODUCT_FOLDER = "product";

  private AppConstant() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
