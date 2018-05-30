import be.jstack.pluginframework.extensionpoint.exception.MQTTServiceException;
import be.jstack.pluginframework.extensionpoint.interfaces.MQTT;
import be.jstack.pluginframework.extensionpoint.mqtt.MQTTService;
import be.jstack.pluginframework.extensionpoint.mqtt.TopicType;
import be.jstack.pluginframework.plugin.Plugin;
import be.jstack.pluginframework.plugin.PluginConfig;
import be.jstack.pluginframework.plugin.PluginState;
import be.jstack.pluginframework.plugin.UserPluginConfig;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

@PluginConfig(name = "MyPlugin", keys = {"api-key"})
public class MyPlugin extends Plugin implements MQTT {

    private MQTTService mqttService = null;

    private final String apiUrl = "http://api.openweathermap.org/data/2.5/weather?units=metric&q=%s&APPID=%s";

    @Override
    public void run() {
    }

    @Override
    public void onMessageReceive(String sender, UserPluginConfig userPluginConfig, JsonObject jsonObject, TopicType topicType) {
        if (state.equals(PluginState.RUNNING) && mqttService != null) {
            final String city = jsonObject.getAsJsonPrimitive("city").getAsString();
            final String apiKey = userPluginConfig.getSettings().get("api-key");
            final JsonObject data = new JsonObject();

            try {
                final double temp = callTemperature(city, apiKey);
                data.addProperty("temperature", temp);
            } catch (DummyPluginException e) {
                e.printStackTrace();
                data.addProperty("error", e.getMessage());
            } finally {
                try {
                    mqttService.publish(topicType, sender, userPluginConfig, data);
                } catch (MQTTServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void init(MQTTService mqttService) {
        this.mqttService = mqttService;
    }

    private double callTemperature(final String city, final String apiKey) throws MyPluginException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(String.format(apiUrl, city, apiKey))
                    .header("accept", "application/json")
                    .asJson();
            return jsonResponse.getBody().getObject().getJSONObject("main").getDouble("temp");
        } catch (Exception e) {
            throw new DummyPluginException("Could not fetch weather data", e);
        }
    }
}
