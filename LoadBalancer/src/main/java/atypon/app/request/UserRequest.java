package atypon.app.request;

import atypon.app.model.User;
import atypon.app.request.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class UserRequest extends ApiRequest {
    private User user;
}
