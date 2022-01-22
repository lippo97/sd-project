package it.unibo.lpaas.core

import it.unibo.lpaas.domain.Goal
import it.unibo.lpaas.domain.GoalId
import it.unibo.lpaas.domain.IncrementalVersion
import it.unibo.lpaas.domain.Subgoal
import it.unibo.lpaas.domain.Theory
import it.unibo.lpaas.domain.TheoryId
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Terms
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.theory.parsing.ClausesParser
import it.unibo.tuprolog.utils.forceCast

object Pokemon {

    val theoryId = TheoryId.of("pokemon")
    val theory2p = ClausesParser.withDefaultOperators.parseTheory("""
        type(Pokemon, Type) :- type(Pokemon, Type, _).
        type(Pokemon, Type) :- type(Pokemon, _, Type).

        type(charmander, fire).
        type(charmeleon, fire).
        type(squirtle, water).
        type(wartortle, water).
        type(blastoise, water).

        type(charizard, fire, flying).
        type(bulbasaur, grass, poison).
        type(ivysaur, grass, poison).
        type(venusaur, grass, poison).

        type(gyarados, water, flying).

        beats(fire, grass).
        beats(water, fire).
        beats(grass, water).
        beats(earth, fire).
        beats(grass, earth).
        beats(water, earth).
        beats(flying, grass).
        beats(rock, fire).
        beats(rock, flying).
        beats(electric, flying).
        beats(electric, water).

        learns(charmander, scratch).
        learns(charmander, growl).
        learns(charmander, ember).
        learns(charmeleon, slash).
        learns(charizard, fly).
        learns(charizard, flamethrower).
        learns(squirtle, surf).
        learns(venusaur, rock_slide).

        move_type(rock_slide, rock).
        move_type(fly, flying).
        move_type(flamethrower, fire).
        move_type(ember, fire).
        move_type(surf, water).


        eventually_becomes(Base, Base).
        eventually_becomes(Base, Evolved) :-
            evolves(Base, Intermediate),
            eventually_becomes(Intermediate, Evolved).

        good_against(Move, Pokemon) :-
            type(Pokemon, PokemonType),
            move_type(Move, Type),
            beats(Type, PokemonType).

        great_against(Move, Pokemon) :-
            type(Pokemon, Type1, Type2),
            move_type(Move, Move_Type),
            beats(Move_Type, Type1),
            beats(Move_Type, Type2).

        evolves(charmander, charmeleon).
        evolves(charmeleon, charizard).
        evolves(squirtle, wartortle).
        evolves(wartortle, blastoise).
        evolves(bulbasaur, ivysaur).
        evolves(ivysaur, venusaur).

        % eventually_becomes(charmander, Pokemon), learns(Pokemon, Move), good_against(Move, venusaur)
        % great_against(Move, charizard)
        % evolves(_, Pokemon), \+ evolves(Pokemon, _)
        % type(Pokemon, T1, T2), beats(Move_Type, T1), beats(Move_Type, T2)
    """.trimIndent())
    val theory = Theory(
        theoryId,
        Theory.Data(theory2p),
        IncrementalVersion.zero
    )

    object Goals {

        fun eventuallyBecomes(pokemon: String): Goal = Goal(
            GoalId.of("eventuallyBecomes_$pokemon"),
            Goal.Data(listOf(Subgoal(Struct.parse("eventually_becomes($pokemon, Pokemon)"))))
        )

        fun eventuallyLearns(pokemon: String): Goal = Goal(
            GoalId.of("eventuallyLearns_$pokemon"),
            Goal.Data(
                listOf(
                    Subgoal(Struct.parse("eventually_becomes($pokemon, Pokemon), learns(Pokemon, Move)")),
                ),
            )
        )

        val intermediateLevelPokemon: Goal = Goal(
            GoalId.of("fullyEvolvedPokemons"),
            Goal.Data(
                listOf(
                    Subgoal(Struct.parse("evolves(_, Pokemon), evolves(Pokemon, _)")),
                )
            )
        )
    }
}
