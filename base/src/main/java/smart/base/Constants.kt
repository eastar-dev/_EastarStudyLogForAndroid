package smart.base

class Constants {
    interface URL {
        companion object {
            const val TEST_COMMON = "/mbp/resource/html/common_list.html"
            const val TEST_SANG01 = "/mbp/resource/html/FFPM/FFPM00/FFPM0001001.html"
            const val TEST_PAYMENT = "/mbp/resource/html/LMNP/LMNP00/LMNP0001001.html"
            const val TEST_IDLOGIN = "/mbp/resource/html/sample/sampleLogin.html"
            const val TEST_ALLMENU = "/mbp/resource/html/sample/common/sampleMenu.html"
            /** 컨텐츠 다운로드 경로 */
            const val CONTENTS_DOWN_URL = "/webapp/hanaplus"
        }
    }

    companion object {
        /** Default Charset  */
        @JvmStatic
        val DEFAULT_CHAR_SET = "UTF-8"

        /** 로컬 호스트 */
        @JvmStatic
        val LOCAL_HSOT = "http://127.0.0.1:8088"

        /**
         * hooking Exception
         */
        const val HOOKING_TXT = "hooking_result.txt"
        const val APP_ROOTING = "/common/setAppCheckErrorLog.do" // 앱루팅 보고
        const val AAND00010 = "AAND00010"
        const val ERR_CD = "errCd="
        const val ERR_MSG = "msg="

        /**
         *  MarketURL
         */
        const val MARKET_URL = "market://details?id=com.kebhana.oneqplus"

        /**
         *  IMQA
         */
        const val IMQA_Key = "\$2a\$05\$CdiR7LUXl1yjFu7ex7xA3eLTFkxn45MRDDkT8LKFpJw1HVY/xW6Dy^#1U8389ATPE/szowZGlK27A=="
        const val IMQA_URL = "http://211.53.25.110:8080"
    }
}

