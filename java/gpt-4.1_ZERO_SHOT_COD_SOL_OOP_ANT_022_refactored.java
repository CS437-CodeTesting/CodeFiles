@Override
public String toString() {
    StringBuilder sb = new StringBuilder("SupervisorInfo(");
    List<String> fields = new ArrayList<>();

    // Helper lambda to append fields in a consistent way
    BiConsumer<String, Object> appendField = (name, value) -> 
        fields.add(name + ":" + (value == null ? "null" : value.toString()));

    // Required field
    appendField.accept("time_secs", this.time_secs);

    // Required field
    appendField.accept("hostname", this.hostname);

    // Optional fields
    if (is_set_assignment_id()) {
        appendField.accept("assignment_id", this.assignment_id);
    }
    if (is_set_used_ports()) {
        appendField.accept("used_ports", this.used_ports);
    }
    if (is_set_meta()) {
        appendField.accept("meta", this.meta);
    }
    if (is_set_scheduler_meta()) {
        appendField.accept("scheduler_meta", this.scheduler_meta);
    }
    if (is_set_uptime_secs()) {
        appendField.accept("uptime_secs", this.uptime_secs);
    }
    if (is_set_version()) {
        appendField.accept("version", this.version);
    }
    if (is_set_resources_map()) {
        appendField.accept("resources_map", this.resources_map);
    }
    if (is_set_server_port()) {
        appendField.accept("server_port", this.server_port);
    }

    sb.append(String.join(", ", fields));
    sb.append(")");
    return sb.toString();
}