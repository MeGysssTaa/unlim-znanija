/*
 * Copyright 2019 DarksideCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.unlimznanija;

public final class HtmlTransformer {

    private HtmlTransformer() {}

    public static String transformAll(String html) {
        html = removeGoogleAds(html);
        html = removePlusAd(html);
        html = removeIFrames(html);

        return html;
    }

    private static String removeGoogleAds(String html) {
        return html.
                replaceAll("<div class=\"brn-ads-box.*?</div>", "").
                replaceAll("<img src=https://.*?\\.googlesyndication.*? == \\$0", "");
    }

    private static String removePlusAd(String html) {
        return html.
                replace("Премиум-доступ<br>со Знаниями Плюс", "ъыыь").
                replace("Начни учиться еще быстрее с неограниченным доступом к ответам от экспертов",
                        "здравствуйте светлана геннадьевна");
    }

    private static String removeIFrames(String html) {
        return html.
                replace("\"iframe_enabled\":true", "\"iframe_enabled\":false").
                replaceAll("<iframe.*?iframe>", "");
    }

}
