package com.sprint.mission.discodeit.exception;

public class DuplicateException extends RuntimeException {
  public DuplicateException(String field, String condition, Object value) {
    super(String.format("필드: %s, 조건: %s, 값: %s", field, condition, value));
  }
}
