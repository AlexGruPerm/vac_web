package comm

case class SimpleDictMeta(dictCode: String,
                          dictName: String,
                          selectType: DictSelectType,
                          defSelectedId: Option[List[Int]],
                          sqlSelect: String)
