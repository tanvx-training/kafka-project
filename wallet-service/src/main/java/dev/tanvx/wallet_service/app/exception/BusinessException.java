package dev.tanvx.wallet_service.app.exception;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class BusinessException extends RuntimeException{
  private Optional<HttpStatus> httpStatus;

  private List<String> messages;

  public BusinessException(HttpStatus httpStatus){
    this.httpStatus = Optional.ofNullable(httpStatus);
  }

  public BusinessException(HttpStatus httpStatus, String message){
    super(message);
    this.httpStatus = Optional.ofNullable(httpStatus);
  }

  public BusinessException(HttpStatus httpStatus, List<String> messages){
    this.httpStatus = Optional.ofNullable(httpStatus);
    this.messages = messages;
  }
}
