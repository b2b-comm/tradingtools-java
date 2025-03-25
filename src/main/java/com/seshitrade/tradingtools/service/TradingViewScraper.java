package com.example.trading.service;

import com.example.trading.model.StockGainer;
import com.example.trading.repository.StockGainerRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TradingViewScraper {

    private static final Logger logger = Logger.getLogger(TradingViewScraper.class.getName());
    private static final String URL = "https://in.tradingview.com/markets/stocks-india/market-movers-gainers/";

    @Autowired
    private StockGainerRepository stockGainerRepository;

    @Scheduled(cron = "0 0 */4 * * *") // Runs every 4 hours
    public void scheduledScrapeAndSaveGainers() {
        logger.info("Scheduled task: Scraping TradingView stock gainers...");
        scrapeAndSaveGainers();
    }

    public void scrapeAndSaveGainers() {
        try {
            logger.info("Starting data scraping from TradingView...");
            Document doc = Jsoup.connect(URL).timeout(10000).get();
            Elements rows = doc.select("table tbody tr");

            if (rows.isEmpty()) {
                logger.warning("No data found on the page! Check the structure of TradingView.");
                return;
            }

            List<StockGainer> gainersList = new ArrayList<>();

            for (Element row : rows) {
                try {
                    Elements cols = row.select("td");

                    if (cols.size() < 4) {
                        logger.warning("Skipping row due to insufficient columns.");
                        continue;
                    }

                    String symbol = cols.get(0).text().trim();
                    String companyName = cols.get(1).text().trim();
                    double lastPrice = parseDouble(cols.get(2).text());
                    double changePercentage = parseDouble(cols.get(3).text().replace("%", ""));

                    if (symbol.isEmpty() || companyName.isEmpty()) {
                        logger.warning("Skipping row due to missing symbol or company name.");
                        continue;
                    }

                    gainersList.add(new StockGainer(symbol, companyName, lastPrice, changePercentage));

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parsing a row, skipping...", e);
                }
            }

            if (!gainersList.isEmpty()) {
                saveGainers(gainersList);
            } else {
                logger.warning("No valid data to save!");
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to connect to TradingView. Check your internet or website changes.", e);
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing number: " + value, e);
            return 0.0;
        }
    }

    private void saveGainers(List<StockGainer> gainersList) {
        for (StockGainer gainer : gainersList) {
            gainer.setDateAdded(new java.util.Date()); // Set timestamp before saving
        }
        try {
            stockGainerRepository.saveAll(gainersList);
            logger.info("Successfully saved " + gainersList.size() + " stock gainers to the database.");
        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Database error while saving stock gainers!", e);
        }
    }
}
