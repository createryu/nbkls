$(function(){
    $("form").submit(check_data);
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
