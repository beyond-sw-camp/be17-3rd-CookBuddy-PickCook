package org.example.be17pickcook.template;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {
    public String getEmailVerificationCompletePage() {
        return """
           <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>이메일 인증 완료</title>
                </head>
                <body>
                    <div style="text-align: center; padding: 50px;">
                        <h2>✅ 이메일 인증이 완료되었습니다!</h2>
                        <p>회원가입 창으로 돌아가서 로그인을 진행해주세요.</p>
                        <p><strong>이 창을 닫으셔도 됩니다.</strong></p>
                        <button onclick="window.close()" style="padding: 10px 20px; font-size: 16px;">
                            창 닫기
                        </button>
                    </div>
                    <script>
                        alert('이메일 인증이 완료되었습니다! 회원가입 창으로 돌아가서 로그인해주세요.');
                        // 3초 후 자동으로 창 닫기 시도
                        setTimeout(() => {
                            window.close();
                        }, 3000);
                    </script>
                </body>
                </html>
           """;
    }
}
