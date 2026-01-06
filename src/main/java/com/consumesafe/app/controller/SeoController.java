// src/main/java/com/consumesafe/app/controller/SeoController.java
package com.consumesafe.app.controller;

import com.consumesafe.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class SeoController {

    @Autowired
    private ProductService productService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        StringBuilder sitemap = new StringBuilder();
        String baseUrl = "https://consumesafe.tn"; // Changez selon votre domaine
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Page d'accueil
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(baseUrl).append("/</loc>\n");
        sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
        sitemap.append("    <changefreq>daily</changefreq>\n");
        sitemap.append("    <priority>1.0</priority>\n");
        sitemap.append("  </url>\n");

        // Liste des produits
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(baseUrl).append("/list</loc>\n");
        sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
        sitemap.append("    <changefreq>weekly</changefreq>\n");
        sitemap.append("    <priority>0.9</priority>\n");
        sitemap.append("  </url>\n");

        // Alternatives
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(baseUrl).append("/alternatives</loc>\n");
        sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
        sitemap.append("    <changefreq>weekly</changefreq>\n");
        sitemap.append("    <priority>0.9</priority>\n");
        sitemap.append("  </url>\n");

        // À propos
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(baseUrl).append("/about</loc>\n");
        sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
        sitemap.append("    <changefreq>monthly</changefreq>\n");
        sitemap.append("    <priority>0.7</priority>\n");
        sitemap.append("  </url>\n");

        sitemap.append("</urlset>");

        return sitemap.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        StringBuilder robots = new StringBuilder();

        robots.append("User-agent: *\n");
        robots.append("Allow: /\n");
        robots.append("Disallow: /api/\n");
        robots.append("\n");
        robots.append("Sitemap: https://consumesafe.tn/sitemap.xml\n");

        return robots.toString();
    }
}