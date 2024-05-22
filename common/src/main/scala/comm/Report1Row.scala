package comm

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class Report1Row(
                            omsu_name: String,
                            org_name: String,
                            ogrn: String,
                            ls_number: String,
                            acc_status: String,
                            gis_error: String,
                            pd_label: String,
                            gis_unique_number: String,
                            pd_status: String,
                            pd_gis_error: String,
                            address: String,
                            rn: Int,
                            total: Int
                           )


