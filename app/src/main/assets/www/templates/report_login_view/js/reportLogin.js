//系统登陆成功，将用户名和密码传到报表服务器中,去开启报表权限
function loginReport(username, password, url, reportState) {
	if (username == "" || password == "" || url == "") {
		return;
	}
	url = url + "/ReportServer?op=fs_load&cmd=sso";
	$.ajax({
		url : url,// url:"http://localhost:8080/WebReport/ReportServer?op=fr_auth&cmd=sso",
		dataType : "jsonp",// 跨域采用jsonp方式
		data : {
			"fr_username" : cjkEncode(username),
			"fr_password" : password
		},
		jsonp : "callback",
		timeout : 5000,// 超时时间（单位：毫秒）
		success : function(data) {
			if (data.status === "success") {
				// 登录成功
                reportState.success = true;
			} else if (data.status === "fail") {
				// 登录失败（用户名或密码错误）
			}

		},
		error : function() {
			// 登录失败（超时或服务器其他错误）
		}
	});
}

// 退出系统时，注销报表服务器方法
function logoutReport(url) {
	if (url == '') {
		return;
	}
	$.ajax({
		url : url + "/ReportServer?op=fs_load&cmd=ssout",// 报表服务器
		dataType : "jsonp",// 跨域采用jsonp方式
		jsonp : "callback",
		async : false,
		timeout : 5000,// 超时时间（单位：毫秒）
		success : function(data) {

		},
		error : function() {

		}
	});
}
function cjkEncode(text) {
    if (text == null) {
        return "";
    }
    var newText = "";
    for (var i = 0; i < text.length; i++) {
        var code = text.charCodeAt (i);
        if (code >= 128 || code == 91 || code == 93) {  //91 is "[", 93 is "]".
            newText += "[" + code.toString(16) + "]";
        } else {
            newText += text.charAt(i);
        }
    }
    return newText;
}