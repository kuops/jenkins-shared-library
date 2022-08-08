package com.shiny.jenkins.pipeline

import groovy.json.JsonSlurper

import java.sql.Array

class CleanKubeNodeLog implements Serializable {
    public static final String THANOS_URL = 'https://thanos.shiny.net'
    def script

    CleanKubeNodeLog(script) {
        this.script = script
    }

    String[] queryHighDataDiskUsageHost() {
        def response = script.httpRequest(
                'url': "${THANOS_URL}/api/v1/query?query=sum+by+%28address%29+%281+-+node_filesystem_free_bytes%7Bcluster_name%3D%7E%22.%2B%22%2Cmountpoint%3D%22%2Fdata%22%7D+%2F+node_filesystem_size_bytes%29+*+100+%3E+70",
                'httpMode': 'GET',
        )
        String responseContent = response.content
        def jsonSlurper = new JsonSlurper()
        def jsonContent = jsonSlurper.parseText(responseContent)
        def jsonContentResult = jsonContent.data.result
        def hosts = []
        if  (jsonContentResult) {
            for (result in jsonContentResult) {
                hosts.add(result.metric.address)
            }
        }
        return hosts
    }
}
