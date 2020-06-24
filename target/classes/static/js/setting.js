$(function(){
    $("#passwordForm").submit(check_data);
    $("#uploadForm").submit(upload);
});

function check_data() {
    var pwd1 = $("#new-password").val();
    var pwd2 = $("#confirm-newpassword").val();
    if(pwd1 != pwd2) {
        $("#confirm-newpassword").addClass("is-invalid");
        return false;
    }
    return true;
}


function upload() {
    $.ajax({
        url: "http://upload.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function(data) {
            if(data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data) {
                        data = $.parseJSON(data);
                        if(data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }
    });
    return false;
}