mosquitto_sub -h 127.0.0.1 -p 1883 -u username -P password -t test

mosquitto_pub -h 127.0.0.1 -p 1883 -u username -P password -t test -m "$(date)"

pub -h 127.0.0.1 -p 1883 -u username -P password -t test -m '{"Type":"DSOS","Cell":[{"CID":75430933,"LAC":43300,"MCC":452,"MNC":4,"SS":-95}],"Conn":"nbiot","APs":[{"SS":-58,"MAC":"B0:95:75:6B:B0:B7"},{"SS":-67,"MAC":"A0:F3:C1:CF:32:F6"},{"SS":-79,"MAC":"C0:A5:DD:14:67:B2"},{"SS":-84,"MAC":"00:1D:0F:CB:35:84"},{"SS":-89,"MAC":"68:FF:7B:21:9F:50"},{"SS":-90,"MAC":"AC:84:C6:B4:40:5A"},{"SS":-92,"MAC":"9C:65:EE:BB:68:AF"},{"SS":-94,"MAC":"B4:EE:B4:61:2F:FA"},{"SS":-95,"MAC":"D8:07:B6:EE:6F:C3"}]}'
