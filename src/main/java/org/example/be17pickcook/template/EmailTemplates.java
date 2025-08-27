package org.example.be17pickcook.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Value("${BACKEND_URL}")
    private String backendUrl;

    // =================================================================
    // 회원가입 인증 이메일 템플릿 (기존 유지)
    // =================================================================

    public String getEmailVerificationTemplate(String email, String uuid) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>이메일 인증 - PickCook</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="text-align: center; color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                    <h2 style="text-align: center; color: #E14345; margin-bottom: 20px;">🎉 가입을 환영합니다!</h2>
                    
                    <p style="text-align: center; color: #333; line-height: 1.6; margin-bottom: 20px;">
                        안녕하세요! <strong>%s</strong>님
                    </p>
                    
                    <p style="text-align: center; color: #333; line-height: 1.6; margin-bottom: 30px;">
                        PickCook 회원가입을 완료하시려면 아래 버튼을 클릭하여 이메일 인증을 완료해주세요.
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s/api/user/verify?uuid=%s" 
                           style="background-color: #E14345; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; display: inline-block; font-weight: bold; font-size: 16px;">
                            ✅ 이메일 인증하기
                        </a>
                    </div>
                    
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-top: 30px; border-left: 4px solid #E14345; text-align: left;">
                        <p style="margin: 0; font-size: 14px; color: #666;">
                            <strong>📌 중요 안내</strong><br>
                            • 이 링크는 24시간 후에 만료됩니다<br>
                            • 본인이 가입하지 않으셨다면 이 이메일을 무시해주세요<br>
                            • 인증 완료 후 로그인이 가능합니다
                        </p>
                    </div>
                </div>
                
                <div style="text-align: center; margin-top: 30px;">
                    <p style="margin: 0; color: #999; font-size: 12px;">
                        이 이메일은 PickCook에서 발송되었습니다.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(email, backendUrl, uuid);
    }

    // =================================================================
    // 비밀번호 재설정 이메일 템플릿 (Vue 연동으로 수정)
    // =================================================================

    public String getPasswordResetEmailTemplate(String email, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>비밀번호 재설정 - PickCook</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="text-align: center; color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                    <h2 style="text-align: center; color: #E14345; margin-bottom: 20px;">🔐 비밀번호 재설정</h2>
                    
                    <p style="text-align: center; color: #333; line-height: 1.6; margin-bottom: 20px;">
                        안녕하세요! <strong>%s</strong>님
                    </p>
                    
                    <p style="text-align: center; color: #333; line-height: 1.6; margin-bottom: 20px;">
                        비밀번호 재설정을 요청하셨습니다.
                    </p>
                    
                    <p style="text-align: center; color: #333; line-height: 1.6; margin-bottom: 30px;">
                        아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.
                    </p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s/reset-password?token=%s" 
                           style="background-color: #E14345; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; display: inline-block; font-weight: bold; font-size: 16px;">
                            🔒 비밀번호 재설정하기
                        </a>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 20px; border-radius: 8px; margin-top: 30px; border-left: 4px solid #E14345; text-align: left;">
                        <p style="margin: 0; font-size: 14px; color: #856404;">
                            <strong>⚠️ 보안 안내</strong><br>
                            • 이 링크는 30분 후에 만료됩니다<br>
                            • 요청하지 않으셨다면 이 이메일을 무시해주세요<br>
                            • 비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다<br>
                            • 변경 완료 후 모든 기기에서 재로그인이 필요합니다
                        </p>
                    </div>
                </div>
                
                <div style="text-align: center; margin-top: 30px;">
                    <p style="margin: 0; color: #999; font-size: 12px;">
                        이 이메일은 PickCook에서 발송되었습니다.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(email, frontendUrl, token);
    }

    // =================================================================
    // 이메일 인증 완료 페이지 (임시 유지 - 추후 Vue로 대체 예정)
    // =================================================================

    public String getEmailVerificationCompletePage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>이메일 인증 완료 - PickCook</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 50px auto; padding: 30px; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center;">
                    <div style="color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                    <div style="font-size: 64px; margin-bottom: 20px;">✅</div>
                    <h2 style="color: #E14345; margin-bottom: 20px;">이메일 인증이 완료되었습니다!</h2>
                    <p style="color: #333; line-height: 1.6; margin-bottom: 30px;">
                        회원가입 창으로 돌아가서 로그인을 진행해주세요.<br>
                        <strong>이 창을 닫으셔도 됩니다.</strong>
                    </p>
                    <button onclick="window.close()" 
                            style="background-color: #E14345; color: white; padding: 12px 24px; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: 600;">
                        창 닫기
                    </button>
                </div>
                
                <script>
                    alert('이메일 인증이 완료되었습니다! 회원가입 창으로 돌아가서 로그인해주세요.');
                    setTimeout(() => {
                        window.close();
                    }, 3000);
                </script>
            </body>
            </html>
            """;
    }

    public String getEmailVerificationErrorPage(String errorMessage) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>이메일 인증 오류 - PickCook</title>
        </head>
        <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 50px auto; padding: 30px; background-color: #f8f9fa;">
            <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center;">
                <div style="color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                <div style="font-size: 64px; margin-bottom: 20px;">❌</div>
                <h2 style="color: #dc3545; margin-bottom: 20px;">이메일 인증에 실패했습니다</h2>
                <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
                    %s
                </p>
                <p style="color: #333; line-height: 1.6; margin-bottom: 30px;">
                    인증 링크가 만료되었거나 이미 사용된 링크일 수 있습니다.<br>
                    회원가입을 다시 진행해주세요.
                </p>
                <button onclick="window.close()" 
                        style="background-color: #6c757d; color: white; padding: 12px 24px; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: 600;">
                    창 닫기
                </button>
            </div>
        </body>
        </html>
        """.formatted(errorMessage);
    }
}