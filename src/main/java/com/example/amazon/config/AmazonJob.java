package com.example.amazon.config;

import com.example.amazon.domain.Sale;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@EnableTask
public class AmazonJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job amazonSalesJob() throws Exception {
        return jobBuilderFactory.get("amazonJob")
            .incrementer(new RunIdIncrementer())
            .start(amazonStep())
            .build();
    }

    @Bean
    public Step amazonStep() throws Exception {
        return stepBuilderFactory.get("amazonStep")
            .<Sale, Sale>chunk(5000)
            .reader(salesReportReader(null))
            .writer(salesReportWriter(null))
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Sale> salesReportReader(
        @Value("#{jobParameters['salesReport']}") Resource inputFile) throws Exception {
        return new FlatFileItemReaderBuilder<Sale>()
            .name("salesReportReader")
            .resource(inputFile)
            .linesToSkip(1)
            .lineTokenizer(salesReportLineTokenizer())
            .fieldSetMapper(salesReportFieldSetMapper())
            .build();
    }

    @Bean
    public LineTokenizer salesReportLineTokenizer() throws Exception {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("id", "orderId", "date", "status", "fulfilment", "salesChannel",
            "shipServiceLevel", "style", "sku", "category", "size", "aSin",
            "courierStatus", "qty", "currency", "amount", "shipCity", "shipState", "shipPostalCode",
            "shipCountry", "promotionIds", "B2B", "fulfilledBy", "unNamed");
        lineTokenizer.afterPropertiesSet();

        return lineTokenizer;
    }

    @Bean
    public FieldSetMapper<Sale> salesReportFieldSetMapper() {
        return fs -> Sale.builder()
            .id(fs.readInt("id"))
            .orderId(fs.readString("orderId"))
            .status(fs.readString("status"))
            .fulfilment(fs.readString("fulfilment"))
            .salesChannel(fs.readString("salesChannel"))
            .shipServiceLevel(fs.readString("shipServiceLevel"))
            .style(fs.readString("style"))
            .sku(fs.readString("sku"))
            .category(fs.readString("category"))
            .size(fs.readString("size"))
            .aSin(fs.readString("aSin"))
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<Sale> salesReportWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Sale>()
            .beanMapped()
            .dataSource(dataSource)
            .sql("INSERT INTO sale (order_id, status, fulfilment, sales_channel, "
                + "ship_service_level, style, sku, category, size, asin) "
                + "VALUES (:orderId, :status, :fulfilment, :salesChannel, :shipServiceLevel, "
                + ":style, :sku, :category, :size, :aSin)")
            .build();
    }
}
