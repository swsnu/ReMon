class DriversController < ApplicationController
  skip_before_action :verify_authenticity_token
  
  def info
    data = {
      :application_id => "app_0",
      :application_name => "HelloREEF",
      :support_aggregation => true,
      :resource_names => [{:resource => "memory"}, {:resource => "cpu"}],
    }
    render :json => data
  end

  def control
    data = {:success => true, :result => "SUCCESS"}
    render :json => data
  end
end
