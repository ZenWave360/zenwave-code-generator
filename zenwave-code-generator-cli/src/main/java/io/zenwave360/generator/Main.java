package io.zenwave360.generator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zenwave360.generator.formatters.Formatter;
import io.zenwave360.generator.generators.Generator;
import io.zenwave360.generator.parsers.Parser;
import io.zenwave360.generator.processors.Processor;
import io.zenwave360.generator.templating.TemplateOutput;
import io.zenwave360.generator.writers.TemplateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author ivangsa
 */
public class Main implements Callable<Integer> {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean help;

    @Option(names = {"-f", "--help-format"}, arity = "0..1", description = "Help output format", defaultValue = "help")
    Help.Format helpFormat = Help.Format.help;

    @Option(names = {"-p", "--plugin"}, arity = "0..1", description = "Plugin Configuration class")
    String pluginConfigClass;

    @Option(names = {"-c", "--chain"}, split = ",", description = "<undocumented> use --plugin instead")
    Class[] chain;

    @CommandLine.Parameters
    Map<String, Object> options = new HashMap<>();

    public static void main(String... args) {
        var main = new Main();
        CommandLine cmd = new CommandLine(main);
        CommandLine.ParseResult parsed  = cmd.parseArgs(args);

        if(parsed.hasMatchedOption("h") && (parsed.hasMatchedOption("p") || parsed.hasMatchedOption("f"))) {
            try {
                main.help();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        int returnCode = cmd.execute(args);
        if(returnCode != 0) {
            System.exit(returnCode);
        }
    }

    @Override
    public Integer call() throws Exception {
        Configuration configuration = Configuration.of(this.pluginConfigClass)
                .withSpecFile((String) options.get("specFile"))
                .withTargetFolder((String) options.get("targetFolder"))
                .withOptions(options)
                .withChain(chain);
        new MainGenerator().generate(configuration);
        return 0;
    }

    public void help() throws Exception {
        Configuration configuration = Configuration.of(this.pluginConfigClass)
                .withSpecFile((String) options.get("specFile"))
                .withTargetFolder((String) options.get("targetFolder"))
                .withOptions(options)
                .withChain(chain);
        String help = new Help().help(configuration, helpFormat);
        System.out.println(help);
    }
}
