package it.unibo.lpaas.core.persistence.repository

interface Repository<Id, Data, Resource> :
    FindAll<Resource>,
    FindByName<Id, Resource>,
    Create<Id, Data, Resource>,
    UpdateByName<Id, Data, Resource>,
    DeleteByName<Id, Resource>
