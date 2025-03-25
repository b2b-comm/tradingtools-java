package com.example.trading.jsf;

import com.example.trading.model.StockGainer;
import com.example.trading.repository.StockGainerRepository;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.ViewScoped;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@ManagedBean
@ViewScoped
@Component
public class StockGainerBean {

    @Autowired
    private StockGainerRepository repository;

    @Getter @Setter
    private List<StockGainer> gainers;

    public void loadGainers() {
        gainers = repository.findAll();
    }
}
