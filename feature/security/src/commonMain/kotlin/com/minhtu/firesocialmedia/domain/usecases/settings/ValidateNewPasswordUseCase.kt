package com.minhtu.firesocialmedia.domain.usecases.settings

import com.minhtu.firesocialmedia.domain.entity.settings.ChangePasswordState
import com.minhtu.firesocialmedia.domain.error.changepassword.ChangePasswordError

class ValidateNewPasswordUseCase() {
    operator fun invoke(newPassword : String,
                        confirmPassword : String) : ChangePasswordState {
        return if(newPassword.isEmpty()) {
            ChangePasswordState(false, ChangePasswordError.DataEmptyError)
        } else{
            if(newPassword != confirmPassword){
                ChangePasswordState(false, ChangePasswordError.PasswordMismatchError)
            } else{
                if(newPassword.length < 6){
                    ChangePasswordState(false, ChangePasswordError.PasswordShortError)
                } else{
                    ChangePasswordState(true)
                }
            }
        }
    }
}
