package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

class TestHttpFileDownload {
    @Test
    void testBatch() {
        String[] testArr = {"https://yavuzceliker.github.io/sample-images/image-10.jpg",
                "https://inews.gtimg.com/om_ls/Of0ctmwDuNFQfhVJDky1P5LlUzglbsWElQc0U3JipX6g0AA_870492/0",
                "https://graydart.com/lib/assets/img/logo/gd_logo.png",
                "https://httpbin.org/bytes/1024"};

        new HttpFileDownload(testArr, "c:/temp/foo").downloadAll();
    }

    @Test
    void testDownload2disk() {
        new HttpFileDownload("https://www.baidu.com/", "c:/temp/foo").download();

        String url = "https://etax.guangdong.chinatax.gov.cn:8443/static_res/images/nlogo14400.png";
        new HttpFileDownload(url, "c:/temp/foo").download();
    }
}
