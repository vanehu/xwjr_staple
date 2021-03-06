package com.xwjr.staple.model

import com.xwjr.staple.extension.isNotNullOrEmpty
import com.xwjr.staple.extension.logE
import com.xwjr.staple.extension.showToast
import com.xwjr.staple.manager.ErrorCodeManager

open class StapleBaseBean {
    var code = ""
    var message = ""
    var error: List<ErrorBean>? = null


    var error_description: String? = null

    fun checkCodeErrorShow(): Boolean {
        return try {
            if (code == "0000") {
                true
            } else {
                when {
                    message.isNotNullOrEmpty() -> showToast(message)
                    error_description.isNotNullOrEmpty() -> showToast(error_description)
                    else -> {
                        if (error != null && error?.size!! > 0) {
                            showToast(ErrorCodeManager.getMessage(error!![0].message, error!![0].message))
                        } else {
                            showToast("发生未知错误")
                        }
                    }
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logE("发生异常，解析网络请求code/message数据异常")
            false
        }
    }

    fun checkCode(): Boolean {
        return try {
            code == "0000"
        } catch (e: Exception) {
            false
        }
    }

    class ErrorBean {
        var message: String? = null
        var type: String? = null
        var value: String? = null
        var code: String? = null
    }
}
