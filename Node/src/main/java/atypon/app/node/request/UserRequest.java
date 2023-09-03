package atypon.app.node.request;

import atypon.app.node.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class UserRequest extends ApiRequest {
    private User user;
}
