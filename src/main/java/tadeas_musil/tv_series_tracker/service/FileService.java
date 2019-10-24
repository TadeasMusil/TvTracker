package tadeas_musil.tv_series_tracker.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.ShowRating;

@Service
public class FileService {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void downloadFile(String url, String targetFile) {
        try {
            FileUtils.copyURLToFile(new URL(url), new File(targetFile));
        } catch (IOException e) {
            logger.error("Unable to download file from {}", url, e);
        }
    }

    public List<ShowRating> parseTsv(String path) {
        BeanListProcessor<ShowRating> processor = new BeanListProcessor<>(ShowRating.class);
        TsvParserSettings settings = new TsvParserSettings();
        settings.setProcessor(processor);
        TsvParser tsvParser = new TsvParser(settings);

        tsvParser.parse(new File(path));

        return processor.getBeans();
    }

    public void decompressGzip(String sourcePath, String targetPath) {

        try (FileInputStream fileInput = new FileInputStream(sourcePath);

                GZIPInputStream gzipInput = new GZIPInputStream(fileInput);

                FileOutputStream fileOutput = new FileOutputStream(new File(targetPath)))

        {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInput.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, length);
            }
        }

        catch (IOException e) {
            logger.error("Unable to decompress Gzip", e);;
        }

    }

}