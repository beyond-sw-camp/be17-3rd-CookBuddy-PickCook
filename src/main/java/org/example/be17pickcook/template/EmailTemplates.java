package org.example.be17pickcook.template;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

    // 🔧 수정: 모든 스타일을 인라인으로 적용
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
                        <a href="http://localhost:8080/api/user/verify?uuid=%s" 
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
            """.formatted(email, uuid);
    }

    // 🔧 수정: 모든 스타일을 인라인으로 적용
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
                        <a href="http://localhost:8080/api/user/reset-password?token=%s" 
                           style="background-color: #E14345; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; display: inline-block; font-weight: bold; font-size: 16px;">
                            🔑 비밀번호 재설정하기
                        </a>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 20px; border-radius: 8px; margin-top: 30px; border-left: 4px solid #E14345; text-align: left;">
                        <p style="margin: 0; font-size: 14px; color: #856404;">
                            <strong>⚠️ 보안 안내</strong><br>
                            • 이 링크는 30분 후에 만료됩니다<br>
                            • 요청하지 않으셨다면 이 이메일을 무시해주세요<br>
                            • 비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다
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
            """.formatted(email, token);
    }

    // 🔧 수정: 모든 스타일을 인라인으로 적용
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

    // 🔧 수정: 모든 스타일을 인라인으로 적용
    public String getPasswordResetPage(String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>비밀번호 재설정 - PickCook</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 50px auto; padding: 30px; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="text-align: center; color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                    <h2 style="text-align: center; color: #333; margin-bottom: 30px;">🔐 새 비밀번호 설정</h2>
                    
                    <form id="resetForm">
                        <input type="hidden" id="token" value="%s">
                        
                        <div style="margin-bottom: 20px;">
                            <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">새 비밀번호</label>
                            <input type="password" id="newPassword" placeholder="새 비밀번호를 입력하세요" required
                                   style="width: 100%%; padding: 12px; border: 2px solid #e1e5e9; border-radius: 6px; font-size: 16px; box-sizing: border-box;">
                            <div style="background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin-top: 10px; font-size: 13px; color: #666; border-left: 4px solid #E14345; text-align: left;">
                                <div id="length" style="margin: 5px 0;">• 8자 이상</div>
                                <div id="letter" style="margin: 5px 0;">• 영문 포함</div>
                                <div id="number" style="margin: 5px 0;">• 숫자 포함</div>
                                <div id="special" style="margin: 5px 0;">• 특수문자 포함 (!@#$%%^&*())</div>
                            </div>
                            <span id="passwordError" style="color: #dc3545; font-size: 14px; margin-top: 8px; display: block;"></span>
                        </div>
                        
                        <div style="margin-bottom: 20px;">
                            <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">비밀번호 확인</label>
                            <input type="password" id="confirmPassword" placeholder="비밀번호를 다시 입력하세요" required
                                   style="width: 100%%; padding: 12px; border: 2px solid #e1e5e9; border-radius: 6px; font-size: 16px; box-sizing: border-box;">
                            <span id="confirmError" style="color: #dc3545; font-size: 14px; margin-top: 8px; display: block;"></span>
                        </div>
                        
                        <button type="submit" id="submitBtn" 
                                style="background-color: #E14345; color: white; padding: 14px 24px; border: none; border-radius: 6px; cursor: pointer; width: 100%%; font-size: 16px; font-weight: 600;">
                            비밀번호 변경
                        </button>
                    </form>
                </div>
                
                <script>
                    const newPasswordInput = document.getElementById('newPassword');
                    const confirmPasswordInput = document.getElementById('confirmPassword');
                    const submitBtn = document.getElementById('submitBtn');
                    
                    newPasswordInput.addEventListener('input', function() {
                        const password = this.value;
                        
                        const lengthReq = document.getElementById('length');
                        if (password.length >= 8) {
                            lengthReq.style.color = '#28a745';
                        } else {
                            lengthReq.style.color = '#dc3545';
                        }
                        
                        const letterReq = document.getElementById('letter');
                        if (/[a-zA-Z]/.test(password)) {
                            letterReq.style.color = '#28a745';
                        } else {
                            letterReq.style.color = '#dc3545';
                        }
                        
                        const numberReq = document.getElementById('number');
                        if (/\\d/.test(password)) {
                            numberReq.style.color = '#28a745';
                        } else {
                            numberReq.style.color = '#dc3545';
                        }
                        
                        const specialReq = document.getElementById('special');
                        if (/[!@#$%%^&*()]/.test(password)) {
                            specialReq.style.color = '#28a745';
                        } else {
                            specialReq.style.color = '#dc3545';
                        }
                        
                        document.getElementById('passwordError').textContent = '';
                    });
                    
                    newPasswordInput.addEventListener('focus', function() {
                        this.style.borderColor = '#E14345';
                    });
                    
                    newPasswordInput.addEventListener('blur', function() {
                        this.style.borderColor = '#e1e5e9';
                    });
                    
                    confirmPasswordInput.addEventListener('focus', function() {
                        this.style.borderColor = '#E14345';
                    });
                    
                    confirmPasswordInput.addEventListener('blur', function() {
                        this.style.borderColor = '#e1e5e9';
                    });
                    
                    submitBtn.addEventListener('mouseenter', function() {
                        if (!this.disabled) {
                            this.style.backgroundColor = '#c13335';
                        }
                    });
                    
                    submitBtn.addEventListener('mouseleave', function() {
                        if (!this.disabled) {
                            this.style.backgroundColor = '#E14345';
                        }
                    });
                    
                    confirmPasswordInput.addEventListener('input', function() {
                        const password = newPasswordInput.value;
                        const confirmPassword = this.value;
                        const confirmError = document.getElementById('confirmError');
                        
                        if (confirmPassword && password !== confirmPassword) {
                            confirmError.textContent = '비밀번호가 일치하지 않습니다.';
                        } else {
                            confirmError.textContent = '';
                        }
                    });
                    
                    document.getElementById('resetForm').addEventListener('submit', async (e) => {
                        e.preventDefault();
                        
                        const token = document.getElementById('token').value;
                        const newPassword = newPasswordInput.value;
                        const confirmPassword = confirmPasswordInput.value;
                        
                        if (newPassword !== confirmPassword) {
                            document.getElementById('confirmError').textContent = '비밀번호가 일치하지 않습니다.';
                            return;
                        }
                        
                        if (newPassword.length < 8 ||
                            !/[a-zA-Z]/.test(newPassword) ||
                            !/\\d/.test(newPassword) ||
                            !/[!@#$%%^&*()]/.test(newPassword)) {
                            document.getElementById('passwordError').textContent = '비밀번호 요구사항을 모두 충족해주세요.';
                            return;
                        }
                        
                        submitBtn.disabled = true;
                        submitBtn.textContent = '처리 중...';
                        submitBtn.style.backgroundColor = '#ccc';
                        submitBtn.style.cursor = 'not-allowed';
                        
                        try {
                            const response = await fetch('/api/user/reset-password', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ token, newPassword, confirmPassword })
                            });
                            
                            const result = await response.json();
                            
                            if (result.success) {
                                alert('🎉 비밀번호가 성공적으로 변경되었습니다!\\n로그인 페이지로 이동합니다.');
                                window.location.href = 'http://localhost:5174/login';
                            } else {
                                alert('⚠ 오류: ' + result.message);
                                submitBtn.disabled = false;
                                submitBtn.textContent = '비밀번호 변경';
                                submitBtn.style.backgroundColor = '#E14345';
                                submitBtn.style.cursor = 'pointer';
                            }
                        } catch (error) {
                            alert('⚠ 요청 처리 중 오류가 발생했습니다.');
                            submitBtn.disabled = false;
                            submitBtn.textContent = '비밀번호 변경';
                            submitBtn.style.backgroundColor = '#E14345';
                            submitBtn.style.cursor = 'pointer';
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(token);
    }

    // 🔧 수정: 모든 스타일을 인라인으로 적용
    public String getPasswordResetErrorPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>오류 - PickCook</title>
            </head>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 50px auto; padding: 30px; text-align: center; background-color: #f8f9fa;">
                <div style="background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <div style="color: #E14345; font-size: 24px; font-weight: bold; margin-bottom: 30px;">PickCook</div>
                    <div style="font-size: 64px; margin-bottom: 20px;">⏰</div>
                    <h2 style="color: #333; margin-bottom: 20px;">링크가 만료되었거나 유효하지 않습니다</h2>
                    <p style="color: #666; line-height: 1.6; margin-bottom: 30px;">
                        비밀번호 재설정 링크가 만료되었거나 이미 사용되었습니다.<br>
                        새로운 링크를 요청해주세요.
                    </p>
                    <div style="margin-top: 30px;">
                        <a href="http://localhost:5174/login" 
                           style="background-color: #E14345; color: white; padding: 12px 24px; border: none; border-radius: 6px; cursor: pointer; text-decoration: none; display: inline-block; font-size: 16px; font-weight: 600;">
                            로그인 페이지로 이동
                        </a>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}