package tadeas_musil.tv_series_tracker.model;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.springframework.core.env.Environment;

import tadeas_musil.tv_series_tracker.util.AppContextUtils;

public class EpisodeDeserializer extends StdDeserializer<Episode> {

    private static final long serialVersionUID = 1L;
    
    private ObjectMapper mapper;
    
    private Environment env;
    
    private TreeNode node;

    protected EpisodeDeserializer(Class<?> vc) {
        super(vc);
        // TODO Auto-generated constructor stub
    }

    protected EpisodeDeserializer() {
        this(Episode.class);
        mapper = new ObjectMapper();
        env = AppContextUtils.getCtx().getBean(Environment.class);
    }

    @Override
    public Episode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        node = p.readValueAsTree();
        String airDate = ZonedDateTime.parse(getValueAtPathOrEmptyString("/first_aired"))
                                      .withZoneSameInstant(ZoneId.of(env.getProperty("app.timezone")))
                                      .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        String season = getValueAtPathOrEmptyString("/episode/season");
        String number = getValueAtPathOrEmptyString("/episode/number");
        String title = getValueAtPathOrEmptyString("/episode/title");
        Show show = node.get("show").traverse(p.getCodec()).readValueAs(Show.class);

        return new Episode(show, season, number, title, airDate);
    }

    private String getValueAtPathOrEmptyString(String path) throws JsonProcessingException {
        TreeNode nodeAtPath = node.at(path);
        if (nodeAtPath.isMissingNode() || nodeAtPath.toString().equals("null")) {
            return "";
        }
        // Using ObjectMapper to get just the value without quotes
        return mapper.treeToValue(nodeAtPath, String.class);
    }

}