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

import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.networking.SampleUserAgents;
import me.darksidecode.kantanj.time.ExpiringList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping ("task")
public class TaskController {

    // can't use TimeUnit - java doesn't recognize its return value as constant and denies compilation
    private static final long CLEANUP_FREQUENCY = 45 /* min */ * 60 /* s */ * 1000 /* ms */;

    private static final int MAX_REQUESTS_PER_10_S = 3;

    private final Map<String, ExpiringList> requestCounters = new ConcurrentHashMap<>();

    @Scheduled (fixedDelay = CLEANUP_FREQUENCY)
    public void doCleanup() {
        System.out.println();
        System.out.println("---- Cleanup! ----");
        System.out.println();

        requestCounters.clear();
    }

    @GetMapping ("{taskId}")
    public String getAnswer(@PathVariable String taskId, HttpServletRequest req) {
        String ip = req.getRemoteAddr();
        ExpiringList reqCounter = requestCounters.computeIfAbsent(ip,
                k -> new ExpiringList(10, TimeUnit.SECONDS));

        if (reqCounter.updateAndCount() > MAX_REQUESTS_PER_10_S)
            return "Слишком много запросов, подожди пару секунд!";

        if ((taskId == null) || (!((taskId = taskId.trim()).
                replaceAll("[0-9]", "").isEmpty())))
            return "ID вопроса указан не верно или не указан вообще (ты сделал(а) что-то не так)";

        System.out.println("Processing request from " + ip + " with taskId = " + taskId);

        try {
            GetHttpRequest request = new GetHttpRequest().
                    baseUrl("https://znanija.com").
                    path("task/" + taskId).
                    userAgent(SampleUserAgents.MOZILLA_WIN_NT).
                    asGetRequest();

            String html = Networking.Http.get(request);
            return HtmlTransformer.transformAll(html);
        } catch (Throwable t) {
            String errId = "#ERR-UZ-" + (System.currentTimeMillis() % 1000) + "_" + taskId;

            System.err.println("Failed to process request: taskId: " + taskId);
            System.err.println("Error ID: " + errId);
            System.err.println("Cause:");

            t.printStackTrace();

            return "Внутренняя ошибка (разраб лох), ID: " + errId;
        }
    }

}
