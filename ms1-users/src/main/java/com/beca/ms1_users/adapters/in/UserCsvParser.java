package com.beca.ms1_users.adapters.in;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;

import com.beca.ms1_users.application.port.in.ImportUserRowCommand;

@Component
public class UserCsvParser {

    private static final Set<String> COLUNAS_OBRIGATORIAS =
            Set.of("nome", "email", "senha");

    public List<ImportUserRowCommand> parse(byte[] content)
            throws IOException {

        if (content == null || content.length == 0) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        String csvContent;

        try (BOMInputStream inputStream = BOMInputStream.builder()
                .setInputStream(new ByteArrayInputStream(content))
                .get()) {

            csvContent = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        String[] linhas = csvContent.split("\\R", -1);

        int indiceCabecalho = encontrarCabecalho(linhas);

        if (indiceCabecalho == -1) {
            throw new IllegalArgumentException(
                    "O arquivo CSV não possui cabeçalho"
            );
        }

        char delimiter = detectDelimiter(linhas[indiceCabecalho]);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setTrim(true)
                .get();

        Map<String, Integer> colunas = lerCabecalho(
                linhas[indiceCabecalho],
                format
        );

        if (!colunas.keySet().containsAll(COLUNAS_OBRIGATORIAS)) {
            throw new IllegalArgumentException(
                    "O CSV precisa conter as colunas: nome, email e senha"
            );
        }

        List<ImportUserRowCommand> usuarios = new ArrayList<>();

        for (int i = indiceCabecalho + 1; i < linhas.length; i++) {

            String linha = linhas[i];

            if (linha.isBlank()) {
                continue;
            }

            int numeroLinhaFisica = i + 1;

            try (CSVParser parserLinha =
                    format.parse(new StringReader(linha))) {

                List<CSVRecord> registros = parserLinha.getRecords();

                int numeroLinha = numeroLinhaFisica
                        + (int) parserLinha.getCurrentLineNumber() - 1;

                if (registros.size() != 1
                        || registros.get(0).size() != colunas.size()) {

                    usuarios.add(linhaInvalida(numeroLinha));
                    continue;
                }

                CSVRecord record = registros.get(0);

                usuarios.add(new ImportUserRowCommand(
                        numeroLinha,
                        record.get(colunas.get("nome")),
                        record.get(colunas.get("email")),
                        record.get(colunas.get("senha"))
                ));

            } catch (Exception exception) {
                usuarios.add(linhaInvalida(numeroLinhaFisica));
            }
        }

        return usuarios;
    }

    private int encontrarCabecalho(String[] linhas) {

        for (int i = 0; i < linhas.length; i++) {
            if (!linhas[i].isBlank()) {
                return i;
            }
        }

        return -1;
    }

    private Map<String, Integer> lerCabecalho(
            String linha,
            CSVFormat format) {

        try (CSVParser parser = format.parse(new StringReader(linha))) {

            List<CSVRecord> registros = parser.getRecords();

            if (registros.size() != 1) {
                throw new IllegalArgumentException(
                        "Cabeçalho CSV inválido"
                );
            }

            CSVRecord record = registros.get(0);
            Map<String, Integer> colunas = new HashMap<>();

            for (int i = 0; i < record.size(); i++) {
                colunas.put(
                        record.get(i).trim().toLowerCase(Locale.ROOT),
                        i
                );
            }

            return colunas;

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Cabeçalho CSV inválido",
                    exception
            );
        }
    }

    private ImportUserRowCommand linhaInvalida(int numeroLinha) {
        return new ImportUserRowCommand(
                numeroLinha,
                null,
                null,
                null
        );
    }

    private char detectDelimiter(String content) {

        long virgulas = content.chars()
                .filter(character -> character == ',')
                .count();

        long pontoEVirgulas = content.chars()
                .filter(character -> character == ';')
                .count();

        return pontoEVirgulas > virgulas ? ';' : ',';
    }
}