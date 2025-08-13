package com.example.demo.controller;

import com.example.demo.dto.CriarPetRequest;
import com.example.demo.model.Pet;
import com.example.demo.repository.PetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "API para gerenciamento de pets")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    @GetMapping
    @Operation(
        summary = "Listar todos os pets",
        description = "Retorna uma lista com todos os pets cadastrados no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pets retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        )
    })
    public ResponseEntity<List<Pet>> listarPets() {
        List<Pet> pets = petRepository.findAll();
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar pet por ID",
        description = "Retorna um pet específico baseado no ID fornecido"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pet encontrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"Pet não encontrado\"")
            )
        )
    })
    public ResponseEntity<Pet> buscarPetPorId(
        @Parameter(description = "ID do pet", example = "1", required = true)
        @PathVariable Long id
    ) {
        Optional<Pet> pet = petRepository.findById(id);
        return pet.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
        summary = "Criar novo pet",
        description = "Cria um novo pet no sistema com os dados fornecidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pet criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"Dados inválidos\"")
            )
        )
    })
    public ResponseEntity<Pet> criarPet(
        @Parameter(description = "Dados do pet a ser criado", required = true)
        @Valid @RequestBody CriarPetRequest request
    ) {
        Pet pet = new Pet(request.getNome(), request.getEspecie(), request.getRaca(), request.getIdade());
        Pet petSalvo = petRepository.save(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(petSalvo);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar pet",
        description = "Atualiza os dados de um pet existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pet atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"Pet não encontrado\"")
            )
        )
    })
    public ResponseEntity<Pet> atualizarPet(
        @Parameter(description = "ID do pet", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "Novos dados do pet", required = true)
        @Valid @RequestBody CriarPetRequest request
    ) {
        Optional<Pet> petExistente = petRepository.findById(id);
        if (petExistente.isPresent()) {
            Pet pet = petExistente.get();
            pet.setNome(request.getNome());
            pet.setEspecie(request.getEspecie());
            pet.setRaca(request.getRaca());
            pet.setIdade(request.getIdade());
            Pet petAtualizado = petRepository.save(pet);
            return ResponseEntity.ok(petAtualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir pet",
        description = "Remove um pet do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Pet excluído com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet não encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"Pet não encontrado\"")
            )
        )
    })
    public ResponseEntity<Void> excluirPet(
        @Parameter(description = "ID do pet", example = "1", required = true)
        @PathVariable Long id
    ) {
        if (petRepository.existsById(id)) {
            petRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/especie/{especie}")
    @Operation(
        summary = "Buscar pets por espécie",
        description = "Retorna todos os pets de uma determinada espécie"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pets da espécie retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        )
    })
    public ResponseEntity<List<Pet>> buscarPorEspecie(
        @Parameter(description = "Espécie dos pets", example = "Cachorro", required = true)
        @PathVariable String especie
    ) {
        List<Pet> pets = petRepository.findByEspecie(especie);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/busca")
    @Operation(
        summary = "Buscar pets por nome",
        description = "Busca pets cujo nome contenha o termo fornecido (case insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pets encontrados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Pet.class)
            )
        )
    })
    public ResponseEntity<List<Pet>> buscarPorNome(
        @Parameter(description = "Termo para busca no nome", example = "Rex", required = true)
        @RequestParam String nome
    ) {
        List<Pet> pets = petRepository.findByNomeContainingIgnoreCase(nome);
        return ResponseEntity.ok(pets);
    }
}
