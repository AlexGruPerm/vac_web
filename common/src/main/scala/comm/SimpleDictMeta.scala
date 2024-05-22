package comm

case class SimpleDictMeta(dictCode: String,
                          dictName: String,
                          selectType: DictSelectType,
                          defSelectedId: Option[Int],
                          sqlSelect: String)
